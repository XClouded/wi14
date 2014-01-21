/*
 * Copyright 2011 Steven Gribble
 *
 *  This file is part of the UW CSE 333 course project sequence
 *  (333proj).
 *
 *  333proj is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  333proj is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with 333proj.  If not, see <http://www.gnu.org/licenses/>.
 */

#include "./FileReader.h"

#include <sys/stat.h>
#include <stdlib.h>
#include <streambuf>

#include <fstream>
#include <iostream>
#include <sstream>
#include <string>

#include "./HttpUtils.h"

using std::cerr;
using std::cout;
using std::endl;
using std::ifstream;
using std::istreambuf_iterator;
using std::string;

namespace hw3 {

bool FileReader::ReadFile(std::string *str) {
  // STEP 1 -- implement ReadFile().
  // test that the filename is under the base
  // directory
  if (!IsPathSafe(basedir_, fname_)) {
    cerr << "Path is not safe." << endl;
    return false;
  }

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
}
