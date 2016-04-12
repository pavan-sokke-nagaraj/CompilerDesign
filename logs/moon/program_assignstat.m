		entry
		lw r1, t0(r0)
		sw PROGRAM_a(r0), r1
		lw r1, PROGRAM_a(r0)
		jl r15, putint
		align
		hlt
		PROGRAM_a	dw 0
		PROGRAM_b	dw 0
		PROGRAM_c	dw 0
		t0		dw 10

