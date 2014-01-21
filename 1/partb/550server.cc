/*
  CSE 550 Problem Set 1
  Wi14
  Jake Sanders
  Pingyang He
*/

#include <iostream> // print
#include <fstream>  // file input
#include <pthread.h>// pthreads

typedef struct ThreadData {
    // file name and connection socket go here
    // gets passed to readFile
} ThreadData;

void readFile(std::string file_name) {
    // the pthread file read routine
    //
    std::cout<<"printting"<<std::endl;
}

int main(int argc, char** argv) {
    // make a threadpool

    // open socket to listen

    // while loop catching incoming connections

    // use condition variables to wake threads-
    // https://computing.llnl.gov/tutorials/pthreads/#ConditionVariables

    // thread reads requested file
    readFile("file name");
    // or ceases execution in the event of read error
    // and is then re-entered into thread pool
}
