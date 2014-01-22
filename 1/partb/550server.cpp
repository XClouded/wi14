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
#include <unistd.h>
#include <sstream>
#include <string>
#include <errno.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <signal.h>
#include <cstring>

using std::cerr;
using std::cout;
using std::endl;
using std::ifstream;
using std::istreambuf_iterator;
using std::string;

extern int errno;

typedef struct ThreadData {
    // file name and connection socket go here
    // gets passed to readFile
	char *fileName;
    int sock;
} ThreadData;

//read file from disk
//First arg is the absolute path of the file,
//Second arg is the pointer where the content of the file will be wrote to.
//The function will return false if it fails to read the file
bool readFile(std::string abs_file_path, std::string *file_content) {
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
}

//write the given buffer to the file descriptor.
//Returns the length that is written to the socket.
int writeToSocket(int fd, string *buf)
{
    int writelen = buf->size();
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

int main(int argc, char** argv) {
    struct sigaction act;
    struct sockaddr_in srv_addr, cli_addr;
    int sckfd, portno, fcntlflags;

    //check for correct # of args
    if (argc != 3) {
        cerr << "Must have exactly 2 arguments." << endl;
        cerr << "Usage:" << endl;
        cerr << argv[0] << " <ipv4 address of server> <listening port>" << endl;
        cerr << "ex: " << argv[0] <<  " 127.0.0.1 9000" << endl;

		exit(0);
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
    srv_addr.sin_addr.s_addr = INADDR_ANY;
    srv_addr.sin_port = htons(portno);
    if (bind(sckfd, (struct sockaddr *) &srv_addr,
             sizeof(srv_addr)) < 0) {
        cerr << "ERROR on binding" << endl;
    }

    listen(sckfd,5);
	
    // make a threadpool

    // open socket to listen

    // while loop catching incoming connections

    // use condition variables to wake threads-
    // https://computing.llnl.gov/tutorials/pthreads/#ConditionVariables

    // thread reads requested file
    string file_content;
    //TODO replace the abs_path
    string abs_path = "/homes/iws/pingyh/550/hw/1/partb/550server.cpp";
    readFile(abs_path, &file_content);

    writeToSocket(sckfd, &file_content);
    // or ceases execution in the event of read error
    // and is then re-entered into thread pool
}
