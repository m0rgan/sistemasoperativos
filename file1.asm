	.data
sum:	.space	4
lock:	.word	0
m:	.word	1000
n:	.word	10000
par2:	.word	1	; file descriptor (1 for standard output)
	.word	sum	; address of the number to write (sum)
	.word	4	; number of bytes to print

	.text
	.main1
	addi	r2,r0,0		; r2 is i, i = 0
	ldw	r4,m(r0)	; r4 is m
loop1:
	cmp	r2,r4		; end of loop1?
	beq	endloop1
startcs1:
	ldw	r6,sum(r0)	; load the counter
	addi	r6,r6,1		; counter++
	stw	sum(r0),r6	; save the result
endcs1:
	addi	r2,r2,1		; i++
	br	loop1		; continue looping
endloop1:
	addi	r11,r0,par2	; prepare to write 
	trap	5
end1:
	ret			; end
	.main2
	addi	r2,r0,0		; r3 is i, i = 0
	ldw	r4,n(r0)	; r5 is n
loop2:
	cmp	r2,r4		; end of loop1?
	beq	endloop2
startcs2:
	ldw	r6,sum(r0)	; load the counter
	addi	r6,r6,1		; counter++
	addi	r7,r7,1		; small delay
	stw	sum(r0),r6	; save the result
endcs2:
	addi	r2,r2,1		; j++
	br	loop2		; continue looping
endloop2:
	addi	r11,r0,par2	; prepare to write 
	trap	5
end2:
	ret			; end
