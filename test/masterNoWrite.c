#include "syscall.h"

int main(void)
{
  
  char *args[] = {(char *)0};

  Exec("test/oneNoWrite", args, 5);
  Exec("test/twoNoWrite", args, 15);
  Exec("test/threeNoWrite", args, 20);
  Exec("test/fourNoWrite", args, 1);
  Exec("test/fiveNoWrite", args, 7);

  Exit(0);
}
