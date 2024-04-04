        .file   "main.c"
        .text
        .globl  main
        .type   main, @function
main:
        pushq   %rbp
        movq    %rsp, %rbp
        movl    $0, -4(%rbp)
        movl    -4(%rbp), %eax
        addl    $1, %eax
        movl    %eax, -8(%rbp)
        movl    -4(%rbp), %eax
        addl    $1, %eax
        movl    %eax, -12(%rbp)
        movl    -4(%rbp), %eax
        addl    $1, %eax
        movl    %eax, -16(%rbp)
        movl    -4(%rbp), %edx
        movl    -8(%rbp), %eax
        addl    %eax, %edx
        movl    -12(%rbp), %eax
        addl    %eax, %edx
        movl    -16(%rbp), %eax
        addl    %edx, %eax
        popq    %rbp
        ret
        .size   main, .-main
        .ident  "GCC: (GNU) 13.2.1 20240316 (Red Hat 13.2.1-7)"
        .section        .note.GNU-stack,"",@progbits
