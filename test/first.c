#include "syscall.h"

void recursive(int x);

int main()
{
  int fd;
  int end= 0;
  char str[] = "BOBS";
  fd = Open("test/wt.txt");
  Write(str, 5, fd);
  Close(fd);

  char str2[5];

  fd = Open("test/wt.txt");
  
  Read(str2, 5, fd);
  Write("creat2\n\r", 8, ConsoleOutput);
  Write(str2, 5, ConsoleOutput);
  Write("creat3\n\r", 8, ConsoleOutput);
  Close(fd);

  recursive(end);

  Write("creat1\n\r", 8, ConsoleOutput);
}

void recursive(int x)
{
	if(x == 1)
		return;
	else
	{
		++x;
		recursive(x);
	}
}
