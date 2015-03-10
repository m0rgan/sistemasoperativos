	.data
	.space 	4
buff1:	.space	4	; where to store the read number
par1:	.word	0	; file descriptor (0 for standard input)
	.word	4	; address to store the number (buff1)
	.word	4	; number of bytes to read
buff2:	.space 	4	; number to print
par2:	.word	1	; file descriptor (1 for standard output)
	.word	20	; address of number to print (buff2)
	.word	4	; number of bytes to print
	.text
main:	
	addi	r11,r0,par1	; 8 is the address of the parameters (par1)
	trap	3		; read
	ldw	r2,buff1(r0)	; load read number into r2
	addi	r2,r2,1		; add 1 to r2
	stw	buff2(r0),r2	; store result in buff2
	addi	r11,r0,par2	; 24 is the address for parameters (par2)
	trap	5		; print
	ret
