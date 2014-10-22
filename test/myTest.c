/* Hello World program */

#include "syscall.h"

//int k = 7;

main()
{
     int i;
     char* args[] = {(char *)0};

    Join(Exec("test/myTest2", args));

    for(i = 0; i < 10; ++i)
      Write("This is test 1!\n\r", 31, ConsoleOutput);

    Exit(0);
}
