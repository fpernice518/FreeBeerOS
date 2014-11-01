#include "syscall.h"

int main(void)
{
	int i;
	for(i = 0; i < 100; i++)
  {
		Write("FOUR\n\r", 6, ConsoleOutput);
	}

  Exit(0);
}
