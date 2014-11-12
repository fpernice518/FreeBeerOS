#include "syscall.h"

int main(void)
{
  
  char *args[] = {(char *)0};

  Exec("oneFileSys", args, 5);
  Exec("twoFileSys", args, 15);
  Exec("threeFileSys", args, 20);
  Exec("fourFileSys", args, 1);
  Exec("fiveFileSys", args, 7);

  Exit(0);
}
