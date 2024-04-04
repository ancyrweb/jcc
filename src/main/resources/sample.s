SECTION .text
global _start
global main
_start:
	call main

	mov rdi, rax
	mov rax, 60
	syscall
main:
	; prologue
	push rbp
	mov rbp, rsp

	mov rax, 1
	
	; epilogue
	pop rbp
	ret