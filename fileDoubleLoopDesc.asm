	.data
sum:	.space	4
m:	.word	100
n:	.word	10
par:	.word	1	; file descriptor (1 for standard output)
	.word	0	; address of number to print (buff2)
	.word	4	; number of bytes to print

	.text
main:	
	addi	r1,r0,0		; r1 is the counter
	ldw	r2,m(r0)	; r2 is i
loop1:
	ldw	r3,n(r0)	; r3 is j
loop2:
	addi	r1,r1,1		; counter++
	subi	r3,r3,1		; end of loop 2?
	cmpi	r3,0
	bne	loop2
endloop2:
	subi	r2,r2,1		; end of loop 1?
	cmpi	r2,0
	bne	loop1
endloop1:
	stw	sum(r0),r1	; save the result
write:
	addi	r11,r0,par	; prepare to write 
	trap	5
end:
	ret			; end



