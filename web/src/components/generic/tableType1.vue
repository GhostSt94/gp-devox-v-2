<template>
	<div class="ui basic segment" style="padding: 0; margin: 0">
		<!--loader-->
		<div class="ui inverted dimmer" :class="{'active': isBusy}">
			<div class="ui text loader">{{$SHARED.consts.loading}}</div>
		</div>
		<!--table-->
		<table class="ui single line table costum-table" style="margin: 0px; padding: 0px">
			<thead>

			<tr>
				<th v-show="selectableRowsNumber">
					<input type="checkbox" @click="selectAll" v-model="allSelected">
				</th>
				<th v-for="(header, index) in headers" :key="index" style="white-space: pre-wrap">
					{{header.label}}
					<i class="ui sort btn icon" @click="$emit('onSort', header.value)" v-if="header.sort"/>
				</th>
				<th v-show="action"></th>
			</tr>
			</thead>

			<tbody>
			<tr v-if="!data.length">
				<td colspan="99">
					<div class="ui header" style="padding: 10px 20px;">{{$SHARED.consts.noData}}</div>
				</td>
			</tr>
			<tr v-else v-for="(entry, index) in data" :key="getKey()">
				<!-- select a row -->
				<td v-show="selectableRowsNumber">
					<input type="checkbox" v-model="entry.selected"
					       v-if="selectable.isRowSelectable(entry)">
				</td>
				<!-- row's data -->
				<td v-for="(header, i) in headers" :key="i" :class="{'collapsing center aligned': header.isHtml}">
					<div v-if="header.isHtml" v-html="getColValue(entry, header)"></div>
					<template v-else>
						{{getColValue(entry, header)}}
					</template>

				</td>
				<!-- action -->
				<slot name="action" v-bind:entry="entry" v-bind:index="index">
					<td v-show="action" @click="onActionClick(entry)" class="collapsing center aligned action">
						<!-- edit icon -->
						<span class="flex justify-center">
				            <svg width="20"
				                 height="20"
				                 viewBox="0 0 30 30"
				                 fill="none"
				                 xmlns="http://www.w3.org/2000/svg">
				              <path d="M20.652 12.89c0-.647-.525-1.171-1.172-1.171H7.41a1.172 1.172 0 100 2.344h12.07c.647 0 1.172-.525 1.172-1.172zM7.41 16.406a1.172 1.172 0 000 2.344h7.33a1.172 1.172 0 100-2.344H7.41z"
				                    fill="#1B4176"></path>
				              <path d="M10.164 27.656h-3.92a2.346 2.346 0 01-2.343-2.343V4.688a2.346 2.346 0 012.344-2.344h14.407a2.346 2.346 0 012.344 2.344v7.207a1.172 1.172 0 102.344 0V4.688A4.693 4.693 0 0020.652 0H6.245a4.693 4.693 0 00-4.688 4.688v20.625A4.693 4.693 0 006.245 30h3.919a1.172 1.172 0 100-2.344z"
				                    fill="#1B4176"></path>
				              <path d="M27.415 16.967a3.52 3.52 0 00-4.97 0l-6.434 6.419a1.171 1.171 0 00-.294.489l-1.401 4.612a1.172 1.172 0 001.434 1.47l4.73-1.31a1.17 1.17 0 00.515-.3l6.42-6.408a3.52 3.52 0 000-4.972zm-7.858 9.504l-2.38.659.697-2.293 4.341-4.331 1.658 1.657-4.316 4.308zm6.202-6.19l-.227.227-1.658-1.658.227-.226a1.173 1.173 0 011.658 1.657zM19.48 7.031H7.41a1.172 1.172 0 100 2.344h12.07a1.172 1.172 0 100-2.344z"
				                    fill="#1B4176"></path>
				            </svg>
				          </span>
					</td>
				</slot>
			</tr>
			</tbody>

			<!-- pagination -->
			<tfoot v-show="showPagination || selectableRowsNumber">
			<tr>
				<th colspan="99">
					<pagination v-if="showPagination" v-bind="pagination" paginationType="basic" @setPage="setPage" style="display: inline-block"/>
					<div v-show="selectableRowsNumber" class="ui tiny right floated blue button"
					     :class="{'disabled': !selectedRows.length}"
					     @click="onSelectableAction($event)">
						<i class="ui tasks icon"/>
						{{selectable.label}}
					</div>
				</th>
			</tr>
			</tfoot>

		</table>
	</div>
</template>

<script>
	const pagination = () => import("@/components/generic/pagination.vue")
	export default {
		name: "tableType1",
		components: {pagination},
		props: {
			headers: {
				type: Array,
				required: true
			},
			data: {
				type: Array,
				required: true
			},
			action: {
				type: Boolean,
				default: true
			},
			pagination: {
				type: Object,
				default() {
					return {
						page: -1,
						limit: -1,
						count: -1,
					}
				}
			},
			isBusy: {
				type: Boolean,
				default: () => false
			},
			selectable: {
				type: Object,
				default: () => {
					return {
						v: false,
						isRowSelectable(row) {
							return false
						},
						label: 'action'
					}
				},
			}
		},
		data() {
			return {
				userSelectedAction: '',
				allSelected: false
			}
		},
		computed: {
			showPagination() {
				return this.pagination.page !== -1
			},
			selectableRowsNumber() {
				return this.selectable?.v && this.data.filter(this.selectable.isRowSelectable).length
			},
			selectedRows() {
				return this.data.filter(e => e.selected);
			}
		},
		watch: {
			selectedRows(newVal, oldVal) {
				const isRowSelected = newVal.length > oldVal.length
				if (this.allSelected && !isRowSelected) {
					this.allSelected = false
				} else if (isRowSelected && this.selectableRowsNumber === this.selectedRows.length) {
					this.allSelected = true
				}
			}
		},
		methods: {
			getKey() {
				return Symbol()
			},
			getColValue(row, header) {
				const PLACE_HOLDER = '';
				try {
					let column = header.value;
					if (column !== '$parent' && column !== "") {
						column.split('.').forEach(element => {
							row = row?.[element];
						});
					}
					if (row !== null && row !== undefined) {
						return 'filter' in header ? header.filter(row) : row;
					} else {
						return PLACE_HOLDER;
					}
				} catch (e) {
					console.error(e)
					return PLACE_HOLDER
				}
			},
			onActionClick(item) {
				this.$emit('onActionClick', item)
			},
			setPage(page) {
				this.$emit('onSetPageClick', page)
			},
			// selectable
			selectAll() {
				if (!this.allSelected)
					this.data.forEach((task) => {
						if (this.selectable.isRowSelectable(task))
							this.$set(task, 'selected', true)
					})
				else
					this.data.forEach((task) => {
						this.$set(task, 'selected', false)
					})
			},
			onSelectableAction(event) {
				this.$emit("onSelectableAction", {
					event,
					rows: this.selectedRows
				})
			}
		}
	}
</script>

<style scoped>
	.action {
		cursor: pointer;
	}
</style>
