#include "./FileReader.h"

#include <sys/stat.h>
#include <stdlib.h>
#include <streambuf>

#include <fstream>
#include <iostream>
#include <sstream>
#include <string>

using std::cerr;
using std::cout;
using std::endl;
using std::ifstream;
using std::istreambuf_iterator;
using std::string;

bool FileReader::ReadFile(std::string *str) {
  int status;
  struct stat st_buf;

  status = stat(fname_.c_str(), &st_buf);
  if (status != 0) {
    cerr << "Error finding status of file system object." << endl;
    return false;
  }

  // test that this file name is actually a file and not a
  // directory
  if (S_ISREG(st_buf.st_mode)) {
    // read the file into a string
    ifstream t(fname_.c_str());
    string file_str((istreambuf_iterator<char>(t)),
        istreambuf_iterator<char>());
    *str = file_str;
    return true;
  } else {
    cerr << "File is not a file but a directory." << endl;
    return false;
  }
}
