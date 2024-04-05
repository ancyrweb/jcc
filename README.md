# JCC

A simple C compiler written in Kotlin.
Outputs GCC compatible NASM.

JCC will not output the `_start` symbol.

## Commands

- `nasm -f elf64 out/test.asm` : convert the ASM to object file
- `gcc -o out/test out/test.o` : creates an executable

# Resources

## C Calling Convention

[Source](https://aaronbloomfield.github.io/pdr/book/x86-64bit-ccc-chapter.pdf)

### Arguments Order

Arguments are passed in registers, in the following order:

- RDI
- RSI
- RDX
- RCX
- R8
- R9

If there are more than 6 parameters to the subroutine, push them onto the stack
*in reverse order* (last parameter first).

### Caller-save registers

The following registers are to be saved by the caller:

- R10
- R11

As well as any registers that are used to pass arguments to the subroutine.
After the call, the caller must clean up the stack from the arguments it pushed.

### Return value

Always in RAX.

## Other

[Stack Frame Layout on x86-64](https://eli.thegreenplace.net/2011/09/06/stack-frame-layout-on-x86-64)
[Why do I even need a red zone ?](https://devblogs.microsoft.com/oldnewthing/20190111-00/?p=100685)