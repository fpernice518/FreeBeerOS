# recursion test (test a)
make recursion

# many concurrent processes test (test b)
make master
make one
make two
make three
make four
make five

# read/write test(test c) & recursion test (test a)
make ioTest
