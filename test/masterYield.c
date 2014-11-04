#include "syscall.h"

int main(void)
{
  
  char *args[] = {(char *)0};

  Exec("test/oneYield", args, 5);
  Exec("test/twoYield", args, 15);
  Exec("test/threeYield", args, 20);
  Exec("test/fourYield", args, 1);
  Exec("test/fiveYield", args, 7);

  Exit(0);
}
