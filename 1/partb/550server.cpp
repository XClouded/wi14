/*
  CSE 550 Problem Set 1
  Wi14
  Jake Sanders
  Pingyang He
*/

#include <cstdlib>
#include <iostream> // print
#include <fstream>  // file input
#include <pthread.h>// pthreads
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <sstream>
#include <string.h>
#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <signal.h>
#include <cstring>
#include <unistd.h>
#include <poll.h>
#include <map>

#define BUSY 0
#define NOT_BUSY 1

#define NUM_PTHREADS 5
#define POLL_TIMEOUT 1000 * 60 * 60 * 24

using std::cerr;
using std::cout;
using std::endl;
using std::ifstream;
using std::istreambuf_iterator;
using std::string;
using std::ios;
using std::map;

extern int errno;

typedef struct ThreadData {
	char *fileName;
    int pipefd;
    pthread_t pthread;
    pthread_mutex_t mutex;
    pthread_cond_t cv;
    int status;
} ThreadData;

//read file from disk
bool readFile(char *abs_file_path, std::string *file_content) {
    // the pthread file read routine
    //
    int status;
    struct stat st_buf;

    status = stat(abs_file_path, &st_buf);
    if (status != 0) {
        cerr << "Error finding status of file system object." << endl;
        return false;
    }

    // test that this file name is actually a file and not a
    // directory
    if (S_ISREG(st_buf.st_mode)) {
    // read the file into a string
        ifstream t(abs_file_path);
        string file_str((istreambuf_iterator<char>(t)), 
                        istreambuf_iterator<char>());
        *file_content = file_str;
        return true;
    } else {
        cerr << "File is not a file but a directory." << endl;
      return false;
    }
    return NULL;
}

string generateResponse(string body)
{
    std::stringstream header;
    int response_code = 200;
    string message("OK");
    header << "HTTP/1.1 " << response_code << " " << message << "\r\n";
    //for (it = headers.begin(); it != headers.end(); it++) {
    //      header << it->first << ": " << it->second << "\r\n";
    //    }
    header << "Content-length: " << body.size() << "\r\n";
    header << "\r\n";
    header << body;
    return header.str();
}

//write the given buffer to the file descriptor.
//Returns the length that is written to the socket.
int writeToSocket(int fd, string *str)
{
    //int writelen = strlen(str);
    int writelen = str->size();
    const char *buf = str->c_str();
    int written_so_far = 0;

    while (written_so_far < writelen) 
    {
        ssize_t res = write(fd,
                      buf + written_so_far,
                      writelen - written_so_far);
        written_so_far += res;

    // Check for disconnection (EOF).
    if (res == 0) {
        break;
    }

    // Check for an error, fatal or otherwise.
    if (res == -1) {
        if ((errno == EAGAIN) || (errno == EINTR))
        continue;

        // Fatal error.
        return -1;
        }
    }

    return written_so_far;
}

char *read_from_fd (int fd)
{
    cout<<"read from fd"<<endl;
    FILE *stream;
    stream = fdopen (fd, "r");
    //fseek(stream, 0, SEEK_END);
    //long pos = ftell(stream);
    //fseek(stream, 0, SEEK_SET);

    //char *bytes = (char *)malloc(pos);
    //fread(bytes, pos, 1, stream);
    //fclose(stream);
    //cout<<bytes<<endl;
    //return bytes;

    int c;
    int size = 8;
    char *buf = (char *)malloc(size);
    char *ptr = buf;
    while ((c = fgetc (stream)) != EOF)
    {
        if ((ptr - buf) >= size - 1)
        {
            char *new_buf = (char *)malloc(size * 2);
            memcpy(new_buf, buf, size);
            free(buf);
            buf = new_buf;
            ptr = new_buf + size - 1;
            size *= 2;
        }
        *ptr = c;
        ptr++;
    }
    fclose (stream);
    *ptr = '\0';
    cout<<buf<<endl;
    return buf;
}

// the worker function for the pthreads
void* fileIOHelper(void* args) {
    ThreadData* td = (ThreadData*)args;

    // lock the mutex for this thread
    pthread_mutex_lock(&td->mutex);
    while (true) {
        td->status = NOT_BUSY;

        // wait for the condition variable to be triggered to do file IO
        pthread_cond_wait(&td->cv, &td->mutex);
        td->status = BUSY;

        // READ FILE GOES HERE
        // GOTTA HAVE THE FILE CACHE READY

        // POKE THE PIPE HERE
    }

    pthread_mutex_unlock(&td->mutex);

    // never gonna get here, but keeps the compiler happy
    return args;
}

string readFromSocket(int socket_fd)
{
    int size = 1024;
    char *buf = (char *)malloc(size);
    int rc = recv(socket_fd, buf, size, 0);
    if (rc < 0)
    {
        if (errno != EWOULDBLOCK)
        {
            perror("  recv() failed");
        }
    }

    if (rc == 0)
    {
        printf("  Connection closed\n");
    }
    //cout<<"rc: "<<rc<<endl;
    //cout<<buf<<endl;
    string result(buf);
    return result;
}

string getRequestedFileName(string req_str)
{
    unsigned int begin, end;
    req_str.find("GET ");
    begin = req_str.find("GET ");

    if (begin != string::npos)
        begin += 4;
    else
        return NULL;

    end = req_str.find(' ', begin);
    return req_str.substr(begin, end-begin);

}

int main(int argc, char** argv) {
    struct sigaction act;
    struct sockaddr_in srv_addr, cli_addr;
    int sckfd, portno, fcntlflags, newsckfd, i, num_fds;
    unsigned int cli_len;
    map <string, string> path_to_file;
    
    ThreadData threads[NUM_PTHREADS];
    int selfpipes[NUM_PTHREADS];
    pthread_attr_t pt_attr;



    //check for correct # of args
    if (argc != 3) {
        cerr << "Must have exactly 2 arguments." << endl;
        cerr << "Usage:" << endl;
        cerr << argv[0] << " <ipv4 address of server> <listening port>" << endl;
        cerr << "ex: " << argv[0] <<  " 127.0.0.1 9000" << endl;

		exit(0);
	}

    // initialize the pthreads
    pthread_attr_init(&pt_attr);
    pthread_attr_setdetachstate(&pt_attr, PTHREAD_CREATE_JOINABLE);

    for(i = 0; i < NUM_PTHREADS; ++i) {
        // initialize the pthread, mutex, and cv
        pthread_mutex_init(&threads[i].mutex, NULL);
        pthread_cond_init (&threads[i].cv, NULL);
        pthread_create(&threads[i].pthread, &pt_attr, fileIOHelper, (void *)(&threads[i]));

        //create a self-pipe
        int pfd[2];
        pipe2(pfd, O_NONBLOCK);
        selfpipes[i] = pfd[0];
        threads[i].pipefd = pfd[1];
    }

    // create a new socket for the server
    if ((sckfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        cerr << "cannot create socket" << endl;

        exit(0);
    }

    // ignore SIGPIPE
    act.sa_handler=SIG_IGN;
    sigemptyset(&act.sa_mask);
    act.sa_flags=0;
    sigaction(SIGPIPE, &act, NULL);
	
    // get the port#
    portno = atoi(argv[2]);

	// set the socket to non-blocking io
    if ((fcntlflags = fcntl(sckfd, F_GETFL, 0)) < 0) {
        fcntlflags = 0;
    }
    fcntl(sckfd, F_SETFL, fcntlflags | O_NONBLOCK);
	
    // bind the socket
    bzero((char *) &srv_addr, sizeof(srv_addr));
    srv_addr.sin_family = AF_INET;
    srv_addr.sin_addr.s_addr = inet_addr(argv[1]);
    srv_addr.sin_port = htons(portno);
    if (bind(sckfd, (struct sockaddr *) &srv_addr,
             sizeof(srv_addr)) < 0) {
        cerr << "ERROR on binding" << endl;
        close(sckfd);
        exit(0);
    }

    cout << "bind done" << endl;

    // listen for incoming connections, limit to 5
    if(listen(sckfd,5) < 0) {
        cerr << "ERROR on listen" << endl;
        close(sckfd);
        exit(0);
    }
    cout << "listen done" << endl;

    // loop, waiting for activity
    num_fds = 1 + NUM_PTHREADS;
    while (true) {
        struct pollfd poll_fd[num_fds];
        memset(poll_fd, 0 , sizeof(poll_fd));
        poll_fd[0].fd = sckfd;
        poll_fd[0].events = POLLIN;
        for(i=0; i<num_fds; ++i) {
            poll_fd[i].events = POLLIN;
        }

        int rv = poll(poll_fd, num_fds, POLL_TIMEOUT);
        if (rv < 0)
        {
            cout<<"poll() failed"<<endl;
            exit(1);
        }
        if (rv == 0)
        {
            cout<<"poll() timeout"<<endl;
            exit(1);
        }

        for(i=0; i<num_fds; ++i) {

        }

        // accept a new incoming connection
        cli_len = sizeof(cli_addr);
        newsckfd = accept(sckfd,
                    (struct sockaddr *) &cli_addr,
                    &cli_len);
        if (newsckfd < 0) {
            cerr << "Error on connection accept" << endl;
        }

        cout << "accept done" << endl;

        string req_str = readFromSocket(newsckfd);
        //char *rqt_str = read_from_fd(newsckfd);
        //cout<<"RQT STR: "<<rqt_str<<endl;
        string file_name = getRequestedFileName(req_str);

        // thread reads requested file
        string relative_path = file_name.insert(0, ".");

        char abs_path[1024];
        realpath(relative_path.c_str(), abs_path);
    }

    

    // TODO: WRITE STUFF OUT TO SOCKET SOMEHOW

    // make a threadpool

    // open socket to listen

    // while loop catching incoming connections

    // use condition variables to wake threads-
    // https://computing.llnl.gov/tutorials/pthreads/#ConditionVariables


    //cout<<"abs path: "<<abs_path<<endl;
    /*
    string file_content;
    bool read_file_success = true;
    if(path_to_file.count(abs_path))
    {
        file_content = path_to_file[abs_path];
    }
    else
    {
       read_file_success = readFile(abs_path, &file_content);
       if(read_file_success) {
           path_to_file[abs_path] = file_content;
       }
    }
    if(read_file_success)
    {
        //cout<<"file_content: "<<file_content<<endl;
        string response = generateResponse(file_content);
        //cout<<"response: "<<response<<endl;
        writeToSocket(newsckfd, &response);
    }
    */
    // or ceases execution in the event of read error
    // and is then re-entered into thread pool
}
