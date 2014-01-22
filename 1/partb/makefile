CC=g++
CFLAGS=-c -Wall

all: 550server

550server: 550server.o
	$(CC) -pthread 550server.o -o 550server

550server.o: 550server.cpp 
	$(CC) $(CFLAGS) -pthread 550server.cpp 
clean:
	rm -rf *o gb
