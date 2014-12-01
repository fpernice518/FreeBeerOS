#include "syscall.h"

void recursive(int x);

int main()
{
  OpenFileId fd;
  int end= 0;
  recursive(end);
  Write("creat1\n\r", 8, ConsoleOutput);
}

void recursive(int x)
{
	if(x == 25)
		return;
	else
	{
		++x;
		recursive(x);
	}
}