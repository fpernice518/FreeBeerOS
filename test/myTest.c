#include "syscall.h"

int main(void)
{
  int i;
  for(i = 0; i < 69; i++)
  {
    Write("TWO\n\r", 5, ConsoleOutput);
  }

  Exit(0);
}
