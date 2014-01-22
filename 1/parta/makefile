CC=gcc
CFLAGS=-c -Wall

all: 550shell

550shell: 550shell.o
	$(CC) 550shell.o -o 550shell

550shell.o: 550shell.c
	$(CC) $(CFLAGS) 550shell.c
clean:
	rm -rf *o gb
