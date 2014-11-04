#include "syscall.h"

int main(void)
{
  
  char *args[] = {(char *)0};

  Exec("test/oneSleep", args, 5);
  Exec("test/twoSleep", args, 15);
  Exec("test/threeSleep", args, 20);
  Exec("test/fourSleep", args, 1);
  Exec("test/fiveSleep", args, 7);

  Exit(0);
}
