.data					;CPU Bound Instructions

	.space 4
	buffer1: .space 3
	par1: .word 1
		  .word 4
		  .word 4

.text
	.main1
		mov r1,r0
		addi r2,r0,100
		addi r3,r0,100

	comp1:
		cmp r2,r0
		bne loop1main1

	end1:
		ret

	loop1main1:
		cmp r3,r0
		bne loop2main1
		addi r3,r0,100
		subi r2,r2,1
		addi r11,r0,8
		trap 3
		br comp1

	loop2main1:
		addi r1,r1,1
		subi r3,r3,1
		br loop1main1

	.main2
		mov r1,r0
		addi r2,r0,500
		addi r3,r0,500

	comp2:
		cmp r2,r0
		bne loop1main2

	end2:
		ret

	loop1main2:
		cmp r3,r0
		bne loop2main2
<<<<<<< HEAD:file2.asm
		addi r3,r0,30
		trap 3
		trap 3
=======
		addi r3,r0,500
>>>>>>> origin/master:file1.asm
		subi r2,r2,1
		trap 3
<<<<<<< HEAD:file2.asm
		trap 3
		addi r11,r0,8
		trap 3
		trap 3
		trap 3
=======
>>>>>>> origin/master:file1.asm
		br comp2

	loop2main2:
		addi r1,r1,1
		trap 3
		trap 3
		subi r3,r3,1
		br loop1main2

	.main3
		mov r1,r0
<<<<<<< HEAD:file2.asm
		addi r2,r0,500
		addi r3,r0,50
		trap 3
		trap 3
=======
		addi r2,r0,100
		addi r3,r0,100
>>>>>>> origin/master:file1.asm

	comp3:
		cmp r2,r0
		bne loop1main3

	end3:
		ret

	loop1main3:
		cmp r3,r0
		bne loop2main3
<<<<<<< HEAD:file2.asm
		trap 3
		addi r3,r0,50
		subi r2,r2,1
		addi r11,r0,8
		trap 3
		trap 3
		trap 3
=======
		addi r3,r0,100
		subi r2,r2,1
		addi r11,r0,8
		trap 3
>>>>>>> origin/master:file1.asm
		br comp3

	loop2main3:
		addi r1,r1,1
		subi r3,r3,1
		br loop1main3

	.main4
		mov r1,r0
<<<<<<< HEAD:file2.asm
		trap 3
		trap 3
		addi r2,r0,100
		addi r3,r0,10
=======
		addi r2,r0,400
		addi r3,r0,400
>>>>>>> origin/master:file1.asm

	comp4:
		cmp r2,r0
		bne loop1main4

	end4:
		trap 3
		ret

	loop1main4:
		cmp r3,r0
		bne loop2main4
		addi r3,r0,400
		subi r2,r2,1
<<<<<<< HEAD:file2.asm
		trap 3
		trap 3
		addi r11,r0,8
		trap 3
		trap 3
=======
		addi r11,r0,8
		trap 3
>>>>>>> origin/master:file1.asm
		br comp4

	loop2main4:
		addi r1,r1,1
		subi r3,r3,1
		br loop1main4

	.main5
		mov r1,r0
		addi r2,r0,500
<<<<<<< HEAD:file2.asm
		addi r3,r0,50
		trap 3
		trap 3
=======
		addi r3,r0,500
>>>>>>> origin/master:file1.asm

	comp5:
		cmp r2,r0
		trap 3
		bne loop1main5
		trap 3

	end5:
		ret

	loop1main5:
		cmp r3,r0
		bne loop2main5
		addi r3,r0,500
		subi r2,r2,1
		addi r11,r0,8
<<<<<<< HEAD:file2.asm
=======
		trap 3
>>>>>>> origin/master:file1.asm
		br comp5

	loop2main5:
		addi r1,r1,1
		subi r3,r3,1
		trap 3
		trap 3
		trap 3
		trap 3
		br loop1main5