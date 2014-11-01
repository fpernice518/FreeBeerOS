#include "syscall.h"

int main(void)
{
	int i;
	for(i = 0; i < 100; i++)
  {
		Write("THREE\n\r", 7, ConsoleOutput);
	}

  Exit(0);
}
