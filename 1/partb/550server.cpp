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
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

typedef struct ThreadData {
    // file name and connection socket go here
    // gets passed to readFile
	char *fileName;
	
} ThreadData;

void readFile(void* arg) {
    // the pthread file read routine
    //
}

int main(int argc, char** argv) {
	if (argc != 3) {
        // fail, not enough args

        std::cerr << "Must have exactly 2 arguments." << std::endl;
        std::cerr << "Usage:" << std::endl;
        std::cerr << argv[0] << " <ipv4 address of server> <listening port>" << std::endl;
        std::cerr << "ex: ./" << argv[0] <<  " 127.0.0.1 9000" << std::endl;

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
