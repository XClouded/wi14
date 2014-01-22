#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <ctype.h>

#define PIPE "|"
#define INPUT_BUFFER_SIZE 1000 
#define STD_IN 0
#define STD_OUT 1

//get input from stdin. 
//If one_line_only is set to be 1, then only get one line(ends with new line character)
char *get_input(int one_line_only);

//trim the given string. Return the new string and free the old string
char *trim_command(char *command);

//process the command
void start(char *input);


int main(int argc, const char *argv[])
{

    char *input = get_input(1);   
    start(input);
    return EXIT_SUCCESS;
}

void start(char *input)
{
    
    int pipe_pos;
    char *search_pointer = input;
    int num_pipe = 0;
    //count the number of pipes
    while((pipe_pos = strcspn(search_pointer, PIPE)) 
        != strlen(search_pointer))
    {
        num_pipe ++;
        search_pointer += pipe_pos + 1;
    }

    int i = 0;
    int num_commands = num_pipe + 1;
    char *commands[num_commands];
    search_pointer = input;
    int pipes[num_pipe][2];
    //save commands in array
    while(1)
    {
        int finished = ((pipe_pos = strcspn(search_pointer, PIPE)) 
                        == strlen(search_pointer));
        char *current_command = strndup(search_pointer, pipe_pos);
        commands[i] = trim_command(current_command);    
        if(strlen(commands[i]) == 0)
        {
            printf("empty command at pipe number %d\n", i + 1);    
            return;
        }
        search_pointer += pipe_pos + 1;
        i++;
        if(finished)
            break;
        
    }

    pid_t pids[num_pipe + 1];
    //run all commands at same time
    for(i = 0; i < num_pipe + 1; i++)
    {
        //creating pipe
        pipe(pipes[i]);

        //fork new process
        pid_t pid = fork();
        pids[i] = pid;
        if(pid != 0)
        { //parent
            if(i < num_pipe)
            {
                //close the std out side of the pipe
                close(pipes[i][STD_OUT]);
            }
        }
        else if(pid == 0)
        {//child
            char *script[] = {commands[i], NULL};

            //setup the output pipe
            if(i < num_pipe)
            {
                close(pipes[i][STD_IN]);
                dup2(pipes[i][STD_OUT], 1);
                
            }
            
            //setup the input pipe
            if(i != 0)
            {
                close(pipes[i - 1][STD_OUT]);
                dup2(pipes[i - 1][STD_IN], 0);
            }

            //execute the given command
            execvp(script[0], script);
            return;
        }    
    }
    //wait for all children to be done
    //waitpid(-1, NULL, 0);
    for(i = 0; i < num_pipe + 1; i++)
    {
        waitpid(pids[i], NULL, 0);
    }
}

char *trim_command(char *str)
{
    char *end;
    char *origin = str;
    
    while(isspace(*str)) str++;//delete leading spaces
  
    if(*str == 0) //str contains only spaces
        return str;
  
    // Trim trailing space
    end = str + strlen(str) - 1;
    while(end > str && isspace(*end)) end--;
  
    // Write new null terminator
    *(end + 1) = '\0';
    char *result = (char *)malloc(strlen(str) + 1);
    memcpy(result, str, strlen(str) + 1);
    //free the original one
    free(origin);
  
    return result;
}

char *get_input(int one_line_only) 
{

    int next_c;
    char *result = malloc(INPUT_BUFFER_SIZE), *resultPtr = result;
    size_t lenmax = INPUT_BUFFER_SIZE, len = lenmax;

    if(result == NULL)
        return NULL;

    do 
    {
        next_c = fgetc(stdin);
        if(next_c == EOF)
            break;

        if(--len == 0) 
        {
            char *new_line = realloc(resultPtr, lenmax *= 2);
            len = lenmax;

            if(new_line == NULL) 
            {
                free(resultPtr);
                return NULL;
            }
            result = new_line + (result - resultPtr);
            resultPtr = new_line;
        }

    }while((*result++ = next_c) != '\n' || !one_line_only);
    *result = '\0';
    return resultPtr;
}

