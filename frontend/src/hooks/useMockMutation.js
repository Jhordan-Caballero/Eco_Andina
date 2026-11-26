import { useMutation, useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import { getErrorMessage } from "@/lib/api"

export function useMockMutation(mutationFn, { success, onSuccess } = {}) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn,
    onSuccess: (data, variables) => {
      queryClient.invalidateQueries()
      if (success) {
        toast.success(typeof success === "function" ? success(data, variables) : success)
      }
      onSuccess?.(data, variables)
    },
    onError: (error) => {
      toast.error(getErrorMessage(error, "No se pudo completar la operación."))
    },
  })
}
