/* Hello World program */

#include "syscall.h"


int main(int argc, char *argv[])
{
    int i;

    // Write(&argv[0], 8, ConsoleOutput);
    // Write(&argv[1], 2, ConsoleOutput);
    // Write(&argv[2], 2, ConsoleOutput);

    for(i = 0; i < 20; ++i)
    {
      Write("This is test 2!\n\r", 31, ConsoleOutput);
      Yield();
    }

    Exit(0);
}
