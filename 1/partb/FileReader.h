#ifndef FILEREADER_H_
#define FILEREADER_H_

#include <string>

// This class is used to read a file into memory and return its
// contents as a string.
class FileReader {
 public:
  FileReader(std::string fname)
    : fname_(fname) { }
  virtual ~FileReader(void) { }

  // Attempts to reads in the file specified by the constructor
  // arguments. If the file could not be found or could not be opened, 
  // returns false.  Otherwise, returns true and also returns the file
  // contents through "str".
  bool ReadFile(std::string *str);

 private:
  std::string fname_;
};

#endif
