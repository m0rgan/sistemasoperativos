.data 
	.space 4
	buffer1: .space 4
	par1: .word 1
		  .word 4
		  .word 4
	buffer2: .space 4
	par2: .word 1
		  .word 20 
		  .word 4
.text
	.main1
		mov r1,r0
		addi r2,r0,100
		addi r3,r0,10

	comp1:
		cmp r2,r0
		bne loop1main1

	print1:
		stw buffer1(r0),r1
		addi r11,r0,8
		trap 5
		ret

	loop1main1:
		cmp r3,r0
		bne loop2main1
		addi r3,r0,10
		subi r2,r2,1
		br comp1

	loop2main1:
		addi r1,r1,1
		subi r3,r3,1
		br loop1main1

	.main2
		mov r4,r0
		addi r5,r0,100
		addi r6,r0,10

	comp2:
		cmp r5,r0
		bne loop1main2

	print2:
		stw buffer2(r0),r4
		addi r11,r0,24
		trap 5
		ret

	loop1main2:
		cmp r6,r0
		bne loop2main2
		addi r6,r0,10
		subi r5,r5,1
		br comp2

	loop2main2:
		addi r4,r4,1
		subi r6,r6,1
		br loop1main2