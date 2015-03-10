	.data
buf:	.space	4
sum:	.space	4
m:	.word	2
n:	.word	3
par1:	.word	0	; file descriptor (0 for standard input)
	.word	buf	; address to read the number (buf)
	.word	4	; number of bytes to print
par2:	.word	1	; file descriptor (1 for standard output)
	.word	sum	; address of the number to write (sum)
	.word	4	; number of bytes to print

	.text
main:	
	addi	r6,r0,0		; r1 is the counter
	ldw	r4,m(r0)	; r4 is m
	ldw	r5,n(r0)	; r5 is n
	addi	r2,r0,0		; r2 is i, i = 0
loop1:
	cmp	r2,r4		; end of loop1?
	beq	endloop1
	addi	r3,r0,0		; r3 is j, j = 0
loop2:
	cmp	r3,r5
	beq	endloop2
body:
	addi	r6,r6,1		; counter++
	addi	r3,r3,1		; j++
	br	loop2
endloop2:
	addi	r11,r0,par1	; prepare to read
	addi	r2,r2,1		; i++
	trap	3
	br	loop1
endloop1:
	stw	sum(r0),r6	; save the result
write:
	addi	r11,r0,par2	; prepare to write 
	trap	5
end:
	ret			; end



