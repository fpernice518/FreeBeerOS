#include "syscall.h"

int main(void)
{
  
  char *args[] = {(char *)0};

  Exec("franksfunzone",args, 3);
  Exec("test/one", args, 5);
  Exec("test/two", args, 15);
  Exec("test/three", args, 20);
  Exec("test/four", args, 1);
  Exec("test/five", args, 7);

  Exit(0);
}
