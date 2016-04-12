entry
		lw r1, t0(r0)
		lw r2, t1(r0)
		add r3, r1, r2
		sw t2(r0), r3
		lw r1, t2(r0)
		lw r2, t3(r0)
		add r3, r1, r2
		sw t4(r0), r3
		lw r1, t4(r0)
		lw r2, t5(r0)
		add r3, r1, r2
		sw t6(r0), r3
		lw r1, t6(r0)
		sw PROGRAM_a(r0), r1
		lw r1, PROGRAM_a(r0)
		jl r15, putint
		hlt
		PROGRAM_a		dw 0
		PROGRAM_b		dw 0
		PROGRAM_c		dw 0
		t0		dw 10
		t1		dw 10
		t2		dw 0
		t3		dw 10
		t4		dw 0
		t5		dw 10
		t6		dw 0
