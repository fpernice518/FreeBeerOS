#include "syscall.h"

int main(void)
{
	int i;
	for(i = 0; i < 100; i++)
  {
		Write("FIVE\n\r", 6, ConsoleOutput);
    Yield();
	}

  Write("DONE FIVE\n\r", 11, ConsoleOutput);
  Exit(0);
}
