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
	if(x == 1)
		return;
	else
	{
		++x;
		recursive(x);
	}
}



 //  Write("create2\n\r", 8, ConsoleOutput);
 //  Write("create3\n\r", 8, ConsoleOutput);
 //  fd = Open("extd");
 //  char buf[25];
 //  int p = 0;
	// for (; p < 25; ++p)
 //  		{
 //   		buf[p] = (char)0;	
 //  		}  
 // 	buf[0] = 'a';
 // 	buf[1] = 'b';
 // 	buf[2] = 'c';


 //  // Read(buf, 25`, fd);
 //  Write(buf, 25, fd);
 //  // Write(fd, 1, ConsoleOutput);
 //  int i = 0;
 //  for (; i < fd; ++i)
 //  {
 //  	Write("count\n\r",7,ConsoleOutput);	
 //  }
 //  Close(fd);
 //  i =0;
 //  for (; p < 25; ++p)
 //  	{
 //   		buf[p] = (char)0;	
 //  	}  
 //  	fd = Open("extd");
 //  	Read(buf, 25, fd);
 //  	Write(buf,4,ConsoleOutput);
 //  	Close(fd);


  //-d + -d f -d t -cp test/open1 open1 -cp test/create-test create -x open1

