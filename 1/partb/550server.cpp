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
#include <signal.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <signal.h>
#include <cstring>
#include <unistd.h>
#include <poll.h>
#include <map>
#include <set>

#define BUSY 0
#define NOT_BUSY 1

#define READ_SUCCESS 1
#define READ_FAILURE 0

#define NUM_PTHREADS 5
#define POLL_TIMEOUT 1000 * 60 * 60 * 24
#define BUFFER_SIZE 1024

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

// global variables

ThreadData threads[NUM_PTHREADS];
int selfpipes[NUM_PTHREADS];
map <string, string> path_to_file;
set<int> open_scks;
pthread_attr_t pt_attr;
int sckfd;

//read file from disk
bool readFile(string abs_file_path, std::string *file_content) {
    // the pthread file read routine
    //
    int status;
    struct stat st_buf;

    status = stat(abs_file_path.c_str(), &st_buf);
    if (status != 0) {
        cerr << "Error finding status of file system object." << endl;
        return false;
    }

    // test that this file name is actually a file and not a
    // directory
    if (S_ISREG(st_buf.st_mode)) {
    // read the file into a string
        ifstream t(abs_file_path.c_str());
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

// tells the thread to kill itself
void killThread(ThreadData &td) {
    pthread_mutex_lock(&td.mutex);
    td.kill = true;
    pthread_cond_signal(&td.cv);
    pthread_mutex_unlock(&td.mutex);
    pthread_join(td.pthread, NULL);
    pthread_mutex_destroy(&td.mutex);
    pthread_cond_destroy(&td.cv);
    close(td.pipefd);
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
    int returned = 0, c = 0;

    //returned = fgetc(stream);
    read(pipe_fd, &returned, 1);
    if (returned == EOF) {
        cerr << "ERROR readByteFromPipe: premature EOF" << endl;
    }

    if (read(pipe_fd, &c, 1) == -1 && errno != EAGAIN) {
        cerr << "ERROR extra bytes in pipe" << endl;
    }

    return returned;
}

// the worker function for the pthreads
void* fileIOHelper(void* args) {
    string file_content;
    ThreadData* td = (ThreadData*)args;
    int result[1];

    // lock the mutex for this thread
    pthread_mutex_lock(&td->mutex);
    while (true) {
        // wait for the condition variable to be triggered to do file IO
        pthread_cond_wait(&td->cv, &td->mutex);

        // if the thread should stop, break
        if (td->kill) break;

        bool read_file_success = true;
        if(path_to_file.count(td->file_path))
        {
            // file already in cache
            file_content = path_to_file[td->file_path];
        }
        else
        {
            // file needs to be read
            read_file_success = readFile(td->file_path, &file_content);
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

        // write the result to the pipe
        write(td->pipefd, result, 1);
    }

    pthread_mutex_unlock(&td->mutex);

    return args;
}

string readFromSocket(int socket_fd)
{
    char *buf = (char *)malloc(BUFFER_SIZE);
    int rc = recv(socket_fd, buf, BUFFER_SIZE, 0);
    char *nl= strstr(buf, "\n");
    if(nl == NULL)
        return NULL;
    if( *(nl - 1) == '\r')
        nl --;
    *nl = '\0';
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

void cleanup() {
    int i;
    set<int>::iterator it;

    cout << "starting cleanup..." << endl;

    // clean up the listening socket
    if (sckfd != NULL) {
        close(sckfd);
        sckfd = NULL;
    }

    // clean up the pthreads
    for(i = 0; i < NUM_PTHREADS; ++i) {
        killThread(threads[i]);
        close(selfpipes[i]);
    }

    // close any open connections
    for (it = open_scks.begin(); it!=open_scks.end(); ++it) {
        close(*it);
    }

    pthread_attr_destroy(&pt_attr);
}

void exit_handler(int sig) {
    cleanup();
    pthread_exit(NULL);
    exit(0);
}

int main(int argc, char** argv) {
    struct sigaction sigIntHandler;
    struct sigaction act;
    struct sockaddr_in srv_addr, cli_addr;
    int portno, fcntlflags, newsckfd, i, num_fds;
    unsigned int cli_len;
    
    sckfd = NULL;

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
        threads[i].status = NOT_BUSY;
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

    // intercept ctrl-c
    sigIntHandler.sa_handler= exit_handler;
    sigemptyset(&sigIntHandler.sa_mask);
    sigIntHandler.sa_flags = 0;
    sigaction(SIGINT, &sigIntHandler, NULL);

	
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

    cout << "server starting" << endl;
    // loop, waiting for activity
    num_fds = 1 + NUM_PTHREADS;
    while (true) {
        struct pollfd poll_fd[num_fds];
        memset(poll_fd, 0 , sizeof(poll_fd));
        poll_fd[0].fd = sckfd;
        poll_fd[0].events = POLLIN;
        for(i=1; i<num_fds; ++i) {
            poll_fd[i].fd = selfpipes[i-1];
            poll_fd[i].events = POLLIN;
        }

        int rv = poll(poll_fd, num_fds, POLL_TIMEOUT);

        if (rv < 0)
        {
            cerr<<"poll() failed"<<endl;
            exit_val = EXIT_FAILURE;
            goto cleanup;
        }

        if (rv == 0)
        {
            cerr<<"poll() timeout"<<endl;
            exit_val = EXIT_FAILURE;
            goto cleanup;
        }

        // poll() returned with an event
        for(i=0; i<num_fds; ++i) {
            // no activity on the FD
            if (poll_fd[i].revents == 0) {
                continue;
            }

            // unknown event on the FD
            if (!(poll_fd[i].revents & POLLIN)) {
                cerr << "ERROR revents = " << poll_fd[i].revents << endl;
                exit_val = EXIT_FAILURE;
                goto cleanup;
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

                    open_scks.insert(newsckfd);

                    // read the request in from the socket
                    string req_str = readFromSocket(newsckfd);

                    // convert the requested file to its realpath
                    char abs_path[BUFFER_SIZE];
                    realpath(req_str.c_str(), abs_path);
                    
                    if (abs_path == 0)
                    {
                        // file not found, close connection and continue
                        closeConnection(newsckfd, open_scks);
                        continue;
                    }

                    string abs_path_str(abs_path);

                    // find an idle thread to read in the file
                    bool threadDispatched = false;
                    int itr = 0;
                    while (!threadDispatched) {
                        // loop waiting for a free thread
                        int index = itr % NUM_PTHREADS;
                        pthread_mutex_lock(&threads[index].mutex);

                        // if the thread is free...
                        if (threads[index].status == NOT_BUSY) {
                            threads[index].status = BUSY;
                            // if the thread is not busy, tell it to get to work!
                            threads[index].file_path = abs_path_str;
                            threads[index].client_sck = newsckfd;
                            pthread_cond_signal(&threads[index].cv);
                            threadDispatched = true;
                        }
                        pthread_mutex_unlock(&threads[index].mutex);
                        itr++;
                    }
                }
            } else if (poll_fd[i].revents == POLLIN) {
                // this is a thread pipe

                // get the result of the file read
                char result = readByteFromPipe(selfpipes[i-1]);
                if (result == READ_SUCCESS) {
                    // write the file back out the socket
                    writeToSocket(threads[i-1].client_sck, &path_to_file[threads[i-1].file_path]);
                }

                // close the connection
                closeConnection(threads[i-1].client_sck, open_scks);
                threads[i-1].status = NOT_BUSY;
                threads[i-1].client_sck = 0;
            }
        }
    }

    cleanup:

    cleanup();
    pthread_exit(NULL);
    return exit_val;
}
