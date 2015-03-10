	.data
sum:	.space	4
m:	.word	10
n:	.word	100
par:	.word	1	; file descriptor (1 for standard output)
	.word	0	; address of number to print (buff2)
	.word	4	; number of bytes to print

	.text
main:	
	addi	r1,r0,0		; r1 is the counter
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
	addi	r1,r1,1		; counter++
	addi	r3,r3,1		; j++
	br	loop2
endloop2:
	addi	r2,r2,1		; i++
	br	loop1
endloop1:
	stw	sum(r0),r1	; save the result
write:
	addi	r11,r0,par	; prepare to write 
	trap	5
end:
	ret			; end



