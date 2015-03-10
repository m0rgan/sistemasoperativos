	.data
vec:	.space	20
sum:	.space	4
	.word	10
	.text
main:	addi	r3,r0,5		; r3 is n (5)
	addi	r2,r0,0		; r2 is i
	addi	r4,r0,1		; r4 is j
data_entry_loop:
	mov	r1,r4
	stw	vec(r2),r1
	addi	r4,r4,1
	addi	r2,r2,4
	subi	r3,r3,1
	cmpi	r3,0
	bne	data_entry_loop
computation:
	addi	r3,r0,5		; r3 = n (5)
	addi	r2,r0,0		; r2 is i
	addi	r4,r0,0		; r4 is sum
loop_sum:
	ldw	r5,vec(r2)
	add	r4,r4,r5
	addi	r2,r2,4
	subi	r3,r3,1
	cmpi	r3,0
	bne	loop_sum
print:
	stw	sum(r0),r4
end:
	ret



