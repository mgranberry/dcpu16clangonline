# Web Service frontend for Clang/DCPU-16 #

This is a simple web app providing a compiler for the DCPU-16 processor.
I have an early beta copy running [here] 
(http://ferret.granberrys.us:8080/Compiler.html) compiling with limited
optimizations.  It's ugly and basic, but features will come.


# A word on security. #
This should be run with caution.  It allows the compilation of arbitrary code,
can #include various system files.  It should be run tucked away in a chroot
jail with normal user privileges and the copy of clang invoked should not capable
of producing X86 binaries.  The server doesn't need any support files, so don't
include the Go environment in the jail.
