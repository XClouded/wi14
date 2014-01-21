CC=g++
CFLAGS=-c -Wall

all: 550server

550server: 550server.o
	$(CC) 550server.o -o 550server

550server.o: 550server.cpp 
	$(CC) $(CFLAGS) 550server.cpp 
clean:
	rm -rf *o gb
