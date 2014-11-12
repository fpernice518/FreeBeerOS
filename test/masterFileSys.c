#include "syscall.h"

int main(void)
{
  
  char *args[] = {(char *)0};

  Exec("one", args, 5);
  Exec("two", args, 15);
  Exec("three", args, 20);
  Exec("four", args, 1);
  Exec("five", args, 7);

  Exit(0);
}
