/* Hello World program */

#include "syscall.h"

//int k = 7;

main()
{
     int i;
     char* args[] = {"-arrrgh\n\r", "-I'm a pirate\n\r",(char *)0};

    Join(Exec("test/myTest2", args));

    for(i = 0; i < 1; ++i)
      Write("This is test 1!\n\r", 17, ConsoleOutput);

    Exit(0);
}
