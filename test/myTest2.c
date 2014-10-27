/* Hello World program */

#include "syscall.h"


int main(int argc, char *argv[])
{
    int i;

    Write(argv[0], 9, ConsoleOutput);
    Write(argv[1], 15, ConsoleOutput);

    for(i = 0; i < 20; ++i)
    {
      Write("This is test 2!\n\r", 17, ConsoleOutput);
      Yield();
    }

    Exit(0);
}
