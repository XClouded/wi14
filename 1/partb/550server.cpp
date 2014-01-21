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

using std::cerr;
using std::cout;
using std::endl;
using std::ifstream;
using std::istreambuf_iterator;
using std::string;

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

int main(int argc, char** argv) {
    int fd;

    //check for correct # of args
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
    string file_content;
    string abs_path = "/homes/iws/pingyh/550/hw/1/partb/550server.cpp";
    readFile(abs_path, &file_content);
    cout<<"file content: " <<file_content<<endl;
    // or ceases execution in the event of read error
    // and is then re-entered into thread pool
}
