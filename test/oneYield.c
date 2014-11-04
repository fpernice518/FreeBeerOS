#include "syscall.h"

int main(void)
{
	int i;
	for(i = 0; i < 100; i++)
  {
		Write("ONE\n\r", 5, ConsoleOutput);
    Yield();
	}

  Write("DONE ONE\n\r", 10, ConsoleOutput);
  Exit(0);
}
