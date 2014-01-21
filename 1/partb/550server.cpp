/*
  CSE 550 Problem Set 1
  Wi14
  Jake Sanders
  Phrazy Pingyang He
*/

#include <cstdlib>
#include <iostream> // print
#include <fstream>  // file input
#include <pthread.h>// pthreads
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

using std::cerr;
using std::cout;
using std::endl;

typedef struct ThreadData {
    // file name and connection socket go here
    // gets passed to readFile
	char *fileName;
    int sock;
} ThreadData;

void readFile(void* arg) {
    // the pthread file read routine
    //
}

int main(int argc, char** argv) {
    int fd;

    // check for correct # of args
    if (argc != 3) {
        cerr << "Must have exactly 2 arguments." << endl;
        cerr << "Usage:" << endl;
        cerr << argv[0] << " <ipv4 address of server> <listening port>" << endl;
        cerr << "ex: " << argv[0] <<  " 127.0.0.1 9000" << endl;

		exit(0);
	}

    // create a new socket for the server
    if ((fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        cerr << "cannot create socket" << endl;

        exit(0);
    }
    // make a threadpool

    // open socket to listen

    // while loop catching incoming connections

    // use condition variables to wake threads-
    // https://computing.llnl.gov/tutorials/pthreads/#ConditionVariables

    // thread reads requested file
    // or ceases execution in the event of read error
    // and is then re-entered into thread pool
}
