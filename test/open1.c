#include "syscall.h"

int main()
{
  OpenFileId fd;
  Write("creat1\n\r", 8, ConsoleOutput);
  Write("create2\n\r", 8, ConsoleOutput);
  Write("create3\n\r", 8, ConsoleOutput);
  fd = Open("create");
  Close(fd);
  //-d + -d f -d t -cp test/open1 open1 -cp test/create-test create -x open1
}
