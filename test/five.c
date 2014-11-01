#include "syscall.h"

int main(void)
{
	int i;
	for(i = 0; i < 100; i++)
  {
		Write("FIVE\n\r", 6, ConsoleOutput);
	}

  Exit(0);
}
