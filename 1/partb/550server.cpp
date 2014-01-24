/*
  CSE 550 Problem Set 1
  Wi14
  Jake Sanders
  Pingyang He
*/

#include <cstdlib>
#include <set>
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

#define READ_SUCCESS 1
#define READ_FAILURE 0

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
using std::set;

extern int errno;

typedef struct ThreadData {
    std::string file_path;
    int pipefd;
    int client_sck;
    pthread_t pthread;
    pthread_mutex_t mutex;
    pthread_cond_t cv;
    int status;
    bool kill;
} ThreadData;

// globally used
map <string, string> path_to_file;

//read file from disk
bool readFile(const char *abs_file_path, std::string *file_content) {
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

void closeConnection(int sck_fd, set<int> &open_scks) {
    open_scks.erase(sck_fd);
    close(sck_fd);
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

char readByteFromPipe (int pipe_fd) {
    FILE *stream;
    int returned, c;
    stream = fdopen (pipe_fd, "r");
    cout<<"read from pipe"<<endl;

    returned = fgetc(stream);
    if (returned == EOF) {
        cerr << "ERROR readByteFromPipe: premature EOF" << endl;
    }

    c = fgetc(stream);
    if(c != EOF) {
        cerr << "ERROR readByteFromPipe: more than 1 byte in pipe" << endl;
    }

    fclose (stream);
    cout << "readByteFromPipe: read = " << returned << endl;
    return returned;
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
    string file_content;
    ThreadData* td = (ThreadData*)args;
    int result[1];

    // lock the mutex for this thread
    pthread_mutex_lock(&td->mutex);
    while (true) {
        td->status = NOT_BUSY;

        // wait for the condition variable to be triggered to do file IO
        pthread_cond_wait(&td->cv, &td->mutex);
        td->status = BUSY;

        // if the thread should stop, break
        if (td->kill) break;

        bool read_file_success = true;
        if(path_to_file.count(td->file_path))
        {
            file_content = path_to_file[td->file_path];
        }
        else
        {
            read_file_success = readFile(td->file_path.c_str(), &file_content);
           if(read_file_success) {
               path_to_file[td->file_path] = file_content;
           }
        }
        if(read_file_success)
        {
            result[0] = READ_SUCCESS;
        }
        else
        {
            result[0] = READ_FAILURE;
        }

        write(td->pipefd, result, 1);
    }

    pthread_mutex_unlock(&td->mutex);

    pthread_exit(NULL);
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

    string result(buf);

    free(buf);
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
    set<int> open_scks;
    set<int>::iterator it;
    struct sigaction act;
    struct sockaddr_in srv_addr, cli_addr;
    int sckfd = NULL, portno, fcntlflags, newsckfd, i, num_fds;
    unsigned int cli_len;
    
    ThreadData threads[NUM_PTHREADS];
    int selfpipes[NUM_PTHREADS];
    pthread_attr_t pt_attr;

    int exit_val = EXIT_SUCCESS;

    //check for correct # of args
    if (argc != 3) {
        cerr << "Must have exactly 2 arguments." << endl;
        cerr << "Usage:" << endl;
        cerr << argv[0] << " <ipv4 address of server> <listening port>" << endl;
        cerr << "ex: " << argv[0] <<  " 127.0.0.1 9000" << endl;

        exit_val = EXIT_FAILURE;
        goto cleanup;
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
        threads[i].kill = false;
        threads[i].client_sck = 0;
    }

    // create a new socket for the server
    if ((sckfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        cerr << "cannot create socket" << endl;
        exit_val = EXIT_FAILURE;
        goto cleanup;
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
        exit_val = EXIT_FAILURE;
        goto cleanup;
    }

    cout << "bind done" << endl;

    // listen for incoming connections, limit to 5
    if(listen(sckfd,5) < 0) {
        cerr << "ERROR on listen" << endl;
        exit_val = EXIT_FAILURE;
        goto cleanup;
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
            close(sckfd);
            exit(0);
        }

        if (rv == 0)
        {
            cout<<"poll() timeout"<<endl;
            close(sckfd);
            exit(0);
        }

        // poll() returned with an event
        for(i=0; i<num_fds; ++i) {
            // no activity on the FD
            if (poll_fd[i].revents == 0) {
                continue;
            }

            // unknown event on the FD
            if (poll_fd[i].revents != POLLIN) {
                cerr << "ERROR revents = " << poll_fd[i].revents << endl;
                exit(0); //don't know what to do!
            }

            // activity on the listening socket
            if (poll_fd[i].fd == sckfd) {
                // this is the listening socket
                while (true) {
                    // accept a new incoming connection
                    cli_len = sizeof(cli_addr);
                    newsckfd = accept(sckfd,
                                (struct sockaddr *) &cli_addr,
                                &cli_len);

                    // no more incoming connections
                    if (newsckfd == -1) break;

                    cout << "accept done" << endl;

                    open_scks.insert(newsckfd);

                    string req_str = readFromSocket(newsckfd);
                    string file_name = getRequestedFileName(req_str);

                    // thread reads requested file
                    string relative_path = file_name.insert(0, ".");

                    char abs_path[1024];
                    realpath(relative_path.c_str(), abs_path);
                    string abs_path_str(abs_path);

                    // find an idle thread
                    bool threadDirpatched = false;
                    int itr = 0;
                    while (true) {
                        // loop waiting for a free thread
                        int index = itr % NUM_PTHREADS;
                        pthread_mutex_lock(&threads[index].mutex);
                        if (threads[index].status == NOT_BUSY) {
                            // if the thread is not busy, tell it to get to work!
                            threads[index].file_path = abs_path_str;
                            pthread_cond_signal(&threads[index].cv);
                            threadDirpatched = true;
                        }
                        pthread_mutex_unlock(&threads[index].mutex);
                        itr++;
                    }
                }
            } else {
                // this is a thread pipe

                // get the result of the file read
                char result = readByteFromPipe(selfpipes[i-1]);
                if (result == READ_SUCCESS) {
                    // write the file back out the socket
                    writeToSocket(threads[i-1].client_sck, &path_to_file[threads[i-1].file_path]);
                }

                // close the connection
                closeConnection(threads[i-1].client_sck, open_scks);
                threads[i-1].client_sck = 0;
            }
        }
    }

    cleanup:

    // clean up the listening socket
    if (sckfd != NULL) {
        close(sckfd);
        sckfd = NULL;
    }

    // clean up the pthreads
    for(i = 0; i < NUM_PTHREADS; ++i) {
        pthread_mutex_lock(&threads[i].mutex);
        threads[i].kill = true;
        pthread_cond_signal(&threads[i].cv);
        pthread_mutex_unlock(&threads[i].mutex);
        pthread_join(threads[i].pthread, NULL);
        pthread_mutex_destroy(&threads[i].mutex);
        pthread_cond_destroy(&threads[i].cv);
    }

    // close any open connections
    for (it = open_scks.begin(); it!=open_scks.end(); ++it) {
        close(*it);
    }

    pthread_attr_destroy(&pt_attr);

    pthread_exit(NULL);

    return exit_val;
}
