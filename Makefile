
CMD = ant

all:
	@$(CMD) debug

release:
	@$(CMD) release

clean:
	@$(CMD) clean

.PHONY: icon

icon:
	@$(CMD) icon
