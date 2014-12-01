#include "syscall.h"

#define BIG_NUMBER 100

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
  if(x == BIG_NUMBER)
    return;
  else
  {
    ++x;
    recursive(x);
  }
}