#include "syscall.h"

int main(void)
{
	int i;
	for(i = 0; i < 10000000; i++)
  {
		Write("ONE\n\r", 5, ConsoleOutput);
	}

  Exit(0);
}
