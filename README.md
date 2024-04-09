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

### Callee's Behavior

#### GCC x86-64

On GCC x86-64, values of 1 and 2 bytes are first stored into a EAX and then
only the lower 8 or 16 bits are copied onto local variables.
Also, these parameters are 4-byte aligned and are stored AFTER the local
variables.

```c
int fn(short a) {
    return 0;
}
```

Would yield

```asm
fn:
        push    rbp
        mov     rbp, rsp
        mov     eax, edi
        mov     WORD PTR [rbp-4], ax
        mov     eax, 0
        pop     rbp
        ret
```

#### ICX x86-64

On the other hands the ICX compiler stores the parameters early on the stack (
before the local variables) and align the local parameters to 8 bytes.

#### ICC x86-64

ICC directly store the content of the parameters into the local variables no
matter their size.

```c

### Return value

Always in RAX.

## Other

[Stack Frame Layout on x86-64](https://eli.thegreenplace.net/2011/09/06/stack-frame-layout-on-x86-64)
[Why do I even need a red zone ?](https://devblogs.microsoft.com/oldnewthing/20190111-00/?p=100685)